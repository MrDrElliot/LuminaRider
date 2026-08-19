package dev.lumina.rider.newclass

enum class LuminaTypeKind(val displayName: String, val prefix: String, val generatesSource: Boolean) {
    OBJECT("Class : CObject", "C", true),
    STRUCT("Reflected struct", "F", false),
    COMPONENT("Entity component", "S", false),
    SYSTEM("Entity system", "S", true),
    ENUM("Reflected enum", "E", false)
}

data class LuminaNewTypeRequest(
    val kind: LuminaTypeKind,
    val typeName: String,
    val fileName: String,
    val namespaceName: String,
    val apiMacro: String,
    val category: String,
    val createSource: Boolean
)

object LuminaTypeGenerator {

    fun header(request: LuminaNewTypeRequest): String {
        val api = if (request.apiMacro.isBlank()) "" else request.apiMacro + " "
        val body = when (request.kind) {
            LuminaTypeKind.OBJECT -> """
                |    REFLECT()
                |    class $api${request.typeName} : public CObject
                |    {
                |        GENERATED_BODY()
                |
                |    public:
                |
                |    };
            """.trimMargin()

            LuminaTypeKind.STRUCT -> """
                |    REFLECT()
                |    struct $api${request.typeName}
                |    {
                |        GENERATED_BODY()
                |
                |        PROPERTY(Editable)
                |        float Value = 0.0f;
                |    };
            """.trimMargin()

            LuminaTypeKind.COMPONENT -> """
                |    REFLECT(Component, Category = "${request.category}")
                |    struct $api${request.typeName}
                |    {
                |        GENERATED_BODY()
                |
                |        PROPERTY(Editable, Category = "${request.category}")
                |        float Value = 0.0f;
                |    };
            """.trimMargin()

            LuminaTypeKind.SYSTEM -> """
                |    REFLECT(System)
                |    struct ${request.typeName}
                |    {
                |        GENERATED_BODY()
                |        ENTITY_SYSTEM(RequiresUpdate(EUpdateStage::PrePhysics))
                |
                |    public:
                |
                |        static FSystemAccess Access;
                |
                |        static void Update(const FSystemContext& SystemContext) noexcept;
                |    };
            """.trimMargin()

            LuminaTypeKind.ENUM -> """
                |    REFLECT()
                |    enum class ${request.typeName} : uint8
                |    {
                |        None,
                |    };
            """.trimMargin()
        }

        val includes = includesFor(request.kind).joinToString("\n") { "#include \"$it\"" }

        return """
            |#pragma once
            |
            |$includes
            |#include "${request.fileName}.generated.h"
            |
            |namespace ${request.namespaceName}
            |{
            |$body
            |}
            |
        """.trimMargin()
    }

    fun source(request: LuminaNewTypeRequest): String {
        val body = when (request.kind) {
            LuminaTypeKind.SYSTEM -> """
                |    FSystemAccess ${request.typeName}::Access = FSystemAccess();
                |
                |    void ${request.typeName}::Update(const FSystemContext& SystemContext) noexcept
                |    {
                |    }
            """.trimMargin()

            else -> ""
        }

        return """
            |#include "${request.fileName}.h"
            |
            |namespace ${request.namespaceName}
            |{
            |$body
            |}
            |
        """.trimMargin()
    }

    private fun includesFor(kind: LuminaTypeKind): List<String> = when (kind) {
        LuminaTypeKind.OBJECT -> listOf("Core/Object/Object.h", "Core/Object/ObjectMacros.h")
        LuminaTypeKind.SYSTEM -> listOf("Core/Object/ObjectMacros.h", "World/Entity/Systems/EntitySystem.h")
        else -> listOf("Core/Object/ObjectMacros.h")
    }
}
