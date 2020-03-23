package org.asciidoc.intellij.actions.intentions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import org.asciidoc.intellij.file.AsciiDocFileType;
import org.asciidoc.intellij.lexer.AsciiDocTokenTypes;
import org.asciidoc.intellij.psi.AsciiDocSection;
import org.asciidoc.intellij.psi.AsciiDocUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AsciiDocAddBlockIdToSection extends Intention {

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    if (editor == null || file == null) {
      return false;
    }
    if (AsciiDocFileType.INSTANCE != file.getFileType()) {
      return false;
    }
    if (editor.getSelectionModel().getSelectedText() != null) {
      return true;
    }
    AsciiDocSection section = getSectionWithoutBlockIdAtCursor(file, editor);
    return section != null;
  }

  @Nullable
  public static AsciiDocSection getSectionWithoutBlockIdAtCursor(PsiFile file, Editor editor) {
    PsiElement statementAtCaret = file.findElementAt(editor.getSelectionModel().getSelectionStart());
    if (statementAtCaret == null) {
      return null;
    }
    while (statementAtCaret instanceof PsiWhiteSpace) {
      statementAtCaret = statementAtCaret.getPrevSibling();
      if (statementAtCaret == null) {
        return null;
      }
    }
    if (statementAtCaret.getNode().getElementType() == AsciiDocTokenTypes.HEADING) {
      statementAtCaret = statementAtCaret.getParent();
    }
    if (!(statementAtCaret instanceof AsciiDocSection)) {
      return null;
    }
    return (AsciiDocSection) statementAtCaret;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
    AsciiDocSection section = getSectionWithoutBlockIdAtCursor(file, editor);
    if (section != null) {
      PsiElement firstChild = section.getFirstChild();
      String id = section.getAutogeneratedId();
      for (PsiElement child : createBlockId(project,
        "[#" + id + "]").getChildren()) {
        section.addBefore(child,
          firstChild);
      }
    }
  }

  @NotNull
  private static PsiElement createBlockId(@NotNull Project project, @NotNull String text) {
    return AsciiDocUtil.createFileFromText(project, text);
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
