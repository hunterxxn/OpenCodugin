package com.github.hunterxxn.opencodugin.commands

object CliProviderRegistry {
    private val providers = mutableListOf<CliProvider>(
        OpenCodeCliProvider,
        MimoCliProvider,
        ReasonixCliProvider
    )

    fun getAll(): List<CliProvider> = providers.toList()

    fun findByCommand(command: String): CliProvider? =
        providers.find { it.defaultCommand == command }

    fun register(provider: CliProvider) {
        providers.add(provider)
    }
}
