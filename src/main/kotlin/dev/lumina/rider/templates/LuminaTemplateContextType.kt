package dev.lumina.rider.templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

class LuminaTemplateContextType : TemplateContextType("Lumina (C++)") {

    override fun isInContext(context: TemplateActionContext): Boolean {
        val name = context.file.virtualFile?.name ?: context.file.name
        val extension = name.substringAfterLast('.', "").lowercase()
        return extension in EXTENSIONS
    }

    private companion object {
        val EXTENSIONS = setOf("h", "hpp", "hxx", "hh", "inl", "ipp", "cpp", "cxx", "cc", "c")
    }
}
