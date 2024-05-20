package com.example.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.pokedex.ui.theme.PokedexTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                DestinationsNavHost(navGraph = NavGraphs.root)
            }
        }
    }
}

@Destination<RootGraph>
@Composable
fun PokemonDetailScreen(
    color: Int,
    name: String
) {
    val dominantColor = remember {
        Color(color)
    }

    val pokemonName = remember {
        name
    }

}