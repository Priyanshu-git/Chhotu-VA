package com.nexxlabs.chhotu.domain.engine

import android.util.Log
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.platform.AppInstallationChecker
import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.domain.registry.Executable
import com.nexxlabs.chhotu.domain.registry.executables.DeepLinkExecutable
import com.nexxlabs.chhotu.domain.registry.model.Action
import com.nexxlabs.chhotu.domain.registry.model.ActionContract
import com.nexxlabs.chhotu.domain.registry.model.CommandResult
import com.nexxlabs.chhotu.domain.registry.model.ExecutionResult
import com.nexxlabs.chhotu.domain.registry.model.RegistryEntry
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CapabilityResolverTest {

    private lateinit var appInstallationChecker: AppInstallationChecker
    private lateinit var appRegistry: AppRegistry
    private lateinit var capabilityResolver: CapabilityResolver

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0

        appInstallationChecker = mockk()
        appRegistry = mockk()

        capabilityResolver = CapabilityResolver(appInstallationChecker, appRegistry)
    }

    @Test
    fun `resolveAndExecute returns ActionNotSupported when intent type is UNKNOWN`() {
        val intent = StructuredIntent(
            intentType = IntentType.UNKNOWN,
            targetApp = "app",
            action = "action",
            entities = emptyMap(),
            confidence = 1.0
        )

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(CommandResult(ExecutionResult.Failure.ActionNotSupported, intent = intent), result)
    }

    @Test
    fun `resolveAndExecute returns MissingRequiredEntities when targetApp is null`() {
        val intent = StructuredIntent(
            intentType = IntentType.OPEN_APP,
            targetApp = null,
            action = "OPEN",
            entities = emptyMap(),
            confidence = 1.0
        )

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(CommandResult(ExecutionResult.Failure.MissingRequiredEntities, intent = intent), result)
    }

    @Test
    fun `resolveAndExecute returns ActionNotSupported when app not found in registry`() {
        val intent = StructuredIntent(
            intentType = IntentType.OPEN_APP,
            targetApp = "unknownapp",
            action = "OPEN",
            entities = emptyMap(),
            confidence = 1.0
        )

        every { appRegistry.findByAlias("unknownapp") } returns null

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(CommandResult(ExecutionResult.Failure.ActionNotSupported, intent = intent), result)
    }

    @Test
    fun `resolveAndExecute returns AppNotInstalled when package not found and no deep link`() {
        val intent = StructuredIntent(
            intentType = IntentType.OPEN_APP,
            targetApp = "myapp",
            action = "OPEN",
            entities = emptyMap(),
            confidence = 1.0
        )

        val openExecutable = mockk<Executable>()

        val registryEntry = RegistryEntry(
            appId = "myapp",
            displayName = "MyApp",
            packageName = "com.myapp",
            aliases = setOf("myapp"),
            actions = setOf(
                Action(
                    id = "OPEN",
                    contract = ActionContract(requiredEntities = emptySet()),
                    primaryExecutable = openExecutable
                )
            )
        )

        every { appRegistry.findByAlias("myapp") } returns registryEntry
        every { appInstallationChecker.isInstalled("com.myapp") } returns false

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(
            CommandResult(
                ExecutionResult.Failure.AppNotInstalled,
                displayName = "MyApp",
                intent = intent
            ),
            result
        )
        verify(exactly = 0) { openExecutable.execute(any()) }
    }

    @Test
    fun `resolveAndExecute executes action successfully when everything is valid`() {
        val intent = StructuredIntent(
            intentType = IntentType.APP_ACTION,
            targetApp = "musicapp",
            action = "PLAY",
            entities = mapOf("song" to "test song"),
            confidence = 1.0
        )

        val executable = mockk<Executable>()
        every { executable.execute(any()) } returns ExecutionResult.Success

        val action = Action(
            id = "PLAY",
            contract = ActionContract(requiredEntities = setOf("song")),
            primaryExecutable = executable
        )

        val registryEntry = RegistryEntry(
            appId = "musicapp",
            displayName = "Music App",
            packageName = "com.musicapp",
            aliases = setOf("musicapp"),
            actions = setOf(action)
        )

        every { appRegistry.findByAlias("musicapp") } returns registryEntry
        every { appInstallationChecker.isInstalled("com.musicapp") } returns true

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(
            CommandResult(
                ExecutionResult.Success,
                displayName = "Music App",
                actionId = "PLAY",
                intent = intent
            ),
            result
        )
        verify { executable.execute(mapOf("song" to "test song")) }
    }

    @Test
    fun `resolveAndExecute falls back to OPEN when action not found`() {
        val intent = StructuredIntent(
            intentType = IntentType.APP_ACTION,
            targetApp = "musicapp",
            action = "DANCE",
            entities = emptyMap(),
            confidence = 1.0
        )

        val openExecutable = mockk<Executable>()
        every { openExecutable.execute(any()) } returns ExecutionResult.Success

        val openAction = Action(
            id = "OPEN",
            contract = ActionContract(requiredEntities = emptySet()),
            primaryExecutable = openExecutable
        )

        val registryEntry = RegistryEntry(
            appId = "musicapp",
            displayName = "Music App",
            packageName = "com.musicapp",
            aliases = setOf("musicapp"),
            actions = setOf(openAction)
        )

        every { appRegistry.findByAlias("musicapp") } returns registryEntry
        every { appInstallationChecker.isInstalled("com.musicapp") } returns true

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(
            CommandResult(
                ExecutionResult.Success,
                displayName = "Music App",
                actionId = "OPEN",
                intent = intent
            ),
            result
        )
        verify { openExecutable.execute(emptyMap()) }
    }

    @Test
    fun `resolveAndExecute returns ActionNotSupported when even OPEN not found`() {
        val intent = StructuredIntent(
            intentType = IntentType.APP_ACTION,
            targetApp = "musicapp",
            action = "DANCE",
            entities = emptyMap(),
            confidence = 1.0
        )

        val registryEntry = RegistryEntry(
            appId = "musicapp",
            displayName = "Music App",
            packageName = "com.musicapp",
            aliases = setOf("musicapp"),
            actions = emptySet()
        )

        every { appRegistry.findByAlias("musicapp") } returns registryEntry
        every { appInstallationChecker.isInstalled("com.musicapp") } returns true

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(
            CommandResult(
                ExecutionResult.Failure.ActionNotSupported,
                displayName = "Music App",
                intent = intent
            ),
            result
        )
    }

    @Test
    fun `resolveAndExecute falls back to OPEN when contract validation fails`() {
        val intent = StructuredIntent(
            intentType = IntentType.APP_ACTION,
            targetApp = "musicapp",
            action = "PLAY",
            entities = emptyMap(),
            confidence = 1.0
        )

        val openExecutable = mockk<Executable>()
        every { openExecutable.execute(any()) } returns ExecutionResult.Success

        val playAction = Action(
            id = "PLAY",
            contract = ActionContract(requiredEntities = setOf("song")),
            primaryExecutable = mockk()
        )

        val openAction = Action(
            id = "OPEN",
            contract = ActionContract(requiredEntities = emptySet()),
            primaryExecutable = openExecutable
        )

        val registryEntry = RegistryEntry(
            appId = "musicapp",
            displayName = "Music App",
            packageName = "com.musicapp",
            aliases = setOf("musicapp"),
            actions = setOf(playAction, openAction)
        )

        every { appRegistry.findByAlias("musicapp") } returns registryEntry
        every { appInstallationChecker.isInstalled("com.musicapp") } returns true

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(
            CommandResult(
                ExecutionResult.Success,
                displayName = "Music App",
                actionId = "OPEN",
                intent = intent
            ),
            result
        )
        verify { openExecutable.execute(emptyMap()) }
    }

    @Test
    fun `resolveAndExecute uses deep link when app not installed and primary is DeepLinkExecutable`() {
        val intent = StructuredIntent(
            intentType = IntentType.APP_ACTION,
            targetApp = "youtube",
            action = "SEARCH",
            entities = mapOf("query" to "kotlin tutorials"),
            confidence = 1.0
        )

        val deepLink = mockk<DeepLinkExecutable>()
        every { deepLink.execute(any()) } returns ExecutionResult.Success

        val searchAction = Action(
            id = "SEARCH",
            contract = ActionContract(requiredEntities = setOf("query")),
            primaryExecutable = deepLink
        )

        val registryEntry = RegistryEntry(
            appId = "youtube",
            displayName = "YouTube",
            packageName = "com.google.android.youtube",
            aliases = setOf("youtube"),
            actions = setOf(searchAction)
        )

        every { appRegistry.findByAlias("youtube") } returns registryEntry
        every { appInstallationChecker.isInstalled("com.google.android.youtube") } returns false

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(
            CommandResult(
                ExecutionResult.Success,
                displayName = "YouTube",
                actionId = "SEARCH",
                intent = intent
            ),
            result
        )
        verify { deepLink.execute(mapOf("query" to "kotlin tutorials")) }
    }

    @Test
    fun `resolveAndExecute uses deep link fallback when app not installed and fallback is DeepLinkExecutable`() {
        val intent = StructuredIntent(
            intentType = IntentType.APP_ACTION,
            targetApp = "google",
            action = "SEARCH",
            entities = mapOf("query" to "weather"),
            confidence = 1.0
        )

        val primaryExecutable = mockk<Executable>()
        val deepLinkFallback = mockk<DeepLinkExecutable>()
        every { deepLinkFallback.execute(any()) } returns ExecutionResult.Success

        val searchAction = Action(
            id = "SEARCH",
            contract = ActionContract(requiredEntities = setOf("query")),
            primaryExecutable = primaryExecutable,
            fallbackExecutable = deepLinkFallback
        )

        val registryEntry = RegistryEntry(
            appId = "google",
            displayName = "Google",
            packageName = "com.google.android.googlequicksearchbox",
            aliases = setOf("google"),
            actions = setOf(searchAction)
        )

        every { appRegistry.findByAlias("google") } returns registryEntry
        every { appInstallationChecker.isInstalled("com.google.android.googlequicksearchbox") } returns false

        val result = capabilityResolver.resolveAndExecute(intent)

        assertEquals(
            CommandResult(
                ExecutionResult.Success,
                displayName = "Google",
                actionId = "SEARCH",
                intent = intent
            ),
            result
        )
        verify { deepLinkFallback.execute(mapOf("query" to "weather")) }
        verify(exactly = 0) { primaryExecutable.execute(any()) }
    }
}
