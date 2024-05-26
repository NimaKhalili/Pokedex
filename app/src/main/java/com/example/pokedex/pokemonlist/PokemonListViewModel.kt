package com.example.pokedex.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.pokedex.data.models.PokedexListEntry
import com.example.pokedex.repository.PokemonRepository
import com.example.pokedex.util.Constants.PAGE_SIZE
import com.example.pokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private var currPage = 0

    var pokemonList = mutableStateOf<List<PokedexListEntry>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)

    private var chachedPokemonList =
        listOf<PokedexListEntry>() //kolle list ra negah midarad ta bad az search 2bare an ra pass bedahad be list asli
    private var isSearchStarting = true // zamani true hast ke SearchBar Empty bashe
    var isSearching =
        mutableStateOf(false) //ta zamani ke field SearchBar dakhelesh text dare isSearching True mishavad

    init {
        loadPokemonPaginated()
    }

    fun searchPokemonList(query: String) {
        val listToSearch = if (isSearchStarting) { //vaghti isSearchStarting empty bashe yani taze search mikhad shoro beshe pas kolle list ro behesh pass midim
            pokemonList.value
        } else {
            chachedPokemonList
        }

        //chera Default ? baraye operation cpu hayi ast ke main thread ra kami bish az har dargir mikonand zira ma dar yek list belghoVVe tolani search mikonim
        viewModelScope.launch(Dispatchers.Default) {
            if (query.isEmpty()){ //yani user text ra az SearchBar delete karde va alan empty hast pas bayad kolle list ro 2bare neshon bedim
                pokemonList.value = chachedPokemonList
                isSearching.value = false //yain dge search nemikonim
                isSearchStarting = true //amade baraye next searche
                return@launch
            }
            else{
                val results = listToSearch.filter {
                    it.pokemonName.contains(query.trim(), ignoreCase = true) || //ignoreCase be uppercase va downercase hasas nmishe
                        it.number.toString() == query.trim() //ba pokemon number ham mishe search kard
                }
                if(isSearchStarting){ //vaghti searchi ra start konim faAl mishe
                    chachedPokemonList = pokemonList.value //ebteda kolle lis ra dar cach save mikonim
                    isSearchStarting = false
                }
                pokemonList.value = results //finally result dar list zakhire mishe //chera dar pokemonList ke list asli hast mirizim ? Chon besorate automat dar lazyColumn neshon mide
                isSearching.value = true
            }
        }
    }

    fun loadPokemonPaginated() {
        viewModelScope.launch {
            val result = repository.getPokemonList(
                PAGE_SIZE,
                currPage * PAGE_SIZE
            )//parameter 2 mgie az kojaye list shoro kone ke migim currPage * PAGE_SIZE yani currPage ebteda 0 ast pass offset 0 mishavad vaghti 20 ta pokemone aval ra load konim offset mishe 20 sepas pokemon 21 ra laod mikonim va be hamin tartib
            when (result) {
                is Resource.Success -> {
                    endReached.value = currPage * PAGE_SIZE >= result.data!!.count
                    val pokedexEntries = result.data.results.mapIndexed { index, entry ->
                        val number = if (entry.url.endsWith("/")) {//age akhari / bod
                            entry.url.dropLast(1)
                                .takeLastWhile { it.isDigit() } //akhari ro hazf kon va baghie ro age digit bod bgir
                        } else {
                            entry.url.takeLastWhile { it.isDigit() }
                        }
                        val url =
                            "https://raw.githubusercontent.com/pokeAPI/sprites/master/sprites/pokemon/${number}.png"
                        PokedexListEntry(entry.name.capitalize(Locale.ROOT), url, number.toInt())
                    }
                    currPage++ //load next page

                    loadError.value = "" //reset error because response is success now
                    isLoading.value = false
                    pokemonList.value += pokedexEntries //ezafe kardan new list be edame list asli
                }

                is Resource.Error -> {
                    loadError.value = result.message!!
                    isLoading.value = false
                }
            }
        }
    }

    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }
}