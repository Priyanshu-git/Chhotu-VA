package com.nexxlabs.chhotu.domain.engine

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import com.nexxlabs.chhotu.domain.engine.ai.model.IntentType
import com.nexxlabs.chhotu.domain.engine.ai.model.StructuredIntent
import com.nexxlabs.chhotu.domain.registry.AppRegistry
import com.nexxlabs.chhotu.domain.registry.Executable
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

        private lateinit var context: Context
        private lateinit var packageManager: PackageManager
        private lateinit var appRegistry: AppRegistry
        private lateinit var capabilityResolver: CapabilityResolver

        @Before
        fun setup() {
                mockkStatic(Log::class)
                every { Log.d(any(), any()) } returns 0
                every { Log.w(any(), any<String>()) } returns 0
                every { Log.e(any(), any()) } returns 0

                context = mockk()
                packageManager = mockk()
                appRegistry = mockk()

                every { context.packageManager } returns packageManager

                capabilityResolver = CapabilityResolver(context, appRegistry)
        }

        @Test
        fun `resolveAndExecute returns ActionNotSupported when intent type is UNKNOWN`() {
                val intent =
                        StructuredIntent(
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
                val intent =
                        StructuredIntent(
                                intentType = IntentType.OPEN_APP,
                                targetApp = null, // Missing
                                action = "OPEN",
                                entities = emptyMap(),
                                confidence = 1.0
                        )

                val result = capabilityResolver.resolveAndExecute(intent)

                assertEquals(CommandResult(ExecutionResult.Failure.MissingRequiredEntities, intent = intent), result)
        }

        @Test
        fun `resolveAndExecute returns ActionNotSupported when app not found in registry`() {
                val intent =
                        StructuredIntent(
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
        fun `resolveAndExecute returns AppNotInstalled when package not found`() {
                val intent =
                        StructuredIntent(
                                intentType = IntentType.OPEN_APP,
                                targetApp = "myapp",
                                action = "OPEN",
                                entities = emptyMap(),
                                confidence = 1.0
                        )

                val registryEntry =
                        RegistryEntry(
                                appId = "myapp",
                                displayName = "MyApp",
                                packageName = "com.myapp",
                                aliases = setOf("myapp"),
                                actions = emptySet()
                        )

                every { appRegistry.findByAlias("myapp") } returns registryEntry
                every { packageManager.getPackageInfo("com.myapp", 0) } throws
                        PackageManager.NameNotFoundException()

                val result = capabilityResolver.resolveAndExecute(intent)

                assertEquals(
                        CommandResult(
                                ExecutionResult.Failure.AppNotInstalled,
                                displayName = "MyApp",
                                intent = intent
                        ),
                        result
                )
        }

        @Test
        fun `resolveAndExecute executes action successfully when everything is valid`() {
                val intent =
                        StructuredIntent(
                                intentType = IntentType.APP_ACTION,
                                targetApp = "musicapp",
                                action = "PLAY",
                                entities = mapOf("song" to "test song"),
                                confidence = 1.0
                        )

                val executable = mockk<Executable>()
                every { executable.execute(any(), any()) } returns ExecutionResult.Success

                val action =
                        Action(
                                id = "PLAY",
                                contract = ActionContract(requiredEntities = setOf("song")),
                                primaryExecutable = executable
                        )

                val registryEntry =
                        RegistryEntry(
                                appId = "musicapp",
                                displayName = "Music App",
                                packageName = "com.musicapp",
                                aliases = setOf("musicapp"),
                                actions = setOf(action)
                        )

                every { appRegistry.findByAlias("musicapp") } returns registryEntry
                every { packageManager.getPackageInfo("com.musicapp", 0) } returns PackageInfo()

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
                verify { executable.execute(context, mapOf("song" to "test song")) }
        }

        @Test
        fun `resolveAndExecute falls back to OPEN when action not found`() {
                val intent =
                        StructuredIntent(
                                intentType = IntentType.APP_ACTION,
                                targetApp = "musicapp",
                                action = "DANCE", // Unknown action
                                entities = emptyMap(),
                                confidence = 1.0
                        )

                val openExecutable = mockk<Executable>()
                every { openExecutable.execute(any(), any()) } returns ExecutionResult.Success

                val openAction =
                        Action(
                                id = "OPEN",
                                contract = ActionContract(requiredEntities = emptySet()),
                                primaryExecutable = openExecutable
                        )

                val registryEntry =
                        RegistryEntry(
                                appId = "musicapp",
                                displayName = "Music App",
                                packageName = "com.musicapp",
                                aliases = setOf("musicapp"),
                                actions = setOf(openAction) // Only OPEN exists
                        )

                every { appRegistry.findByAlias("musicapp") } returns registryEntry
                every { packageManager.getPackageInfo("com.musicapp", 0) } returns PackageInfo()

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
                verify { openExecutable.execute(context, emptyMap()) }
        }

        @Test
        fun `resolveAndExecute returns ActionNotSupported when even OPEN not found`() {
                val intent =
                        StructuredIntent(
                                intentType = IntentType.APP_ACTION,
                                targetApp = "musicapp",
                                action = "DANCE",
                                entities = emptyMap(),
                                confidence = 1.0
                        )

                val registryEntry =
                        RegistryEntry(
                                appId = "musicapp",
                                displayName = "Music App",
                                packageName = "com.musicapp",
                                aliases = setOf("musicapp"),
                                actions = emptySet() // No actions
                        )

                every { appRegistry.findByAlias("musicapp") } returns registryEntry
                every { packageManager.getPackageInfo("com.musicapp", 0) } returns PackageInfo()

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
                val intent =
                        StructuredIntent(
                                intentType = IntentType.APP_ACTION,
                                targetApp = "musicapp",
                                action = "PLAY",
                                entities = emptyMap(), // Missing "song"
                                confidence = 1.0
                        )

                val openExecutable = mockk<Executable>()
                every { openExecutable.execute(any(), any()) } returns ExecutionResult.Success

                val playAction =
                        Action(
                                id = "PLAY",
                                contract =
                                        ActionContract(
                                                requiredEntities = setOf("song")
                                        ), // Requires "song"
                                primaryExecutable = mockk()
                        )

                val openAction =
                        Action(
                                id = "OPEN",
                                contract = ActionContract(requiredEntities = emptySet()),
                                primaryExecutable = openExecutable
                        )

                val registryEntry =
                        RegistryEntry(
                                appId = "musicapp",
                                displayName = "Music App",
                                packageName = "com.musicapp",
                                aliases = setOf("musicapp"),
                                actions = setOf(playAction, openAction)
                        )

                every { appRegistry.findByAlias("musicapp") } returns registryEntry
                every { packageManager.getPackageInfo("com.musicapp", 0) } returns PackageInfo()

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
                verify { openExecutable.execute(context, emptyMap()) }
        }
}
