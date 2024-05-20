package com.example.pokedex.repository

import com.example.pokedex.data.remote.PokeApi
import com.example.pokedex.data.remote.responses.Pokemon
import com.example.pokedex.data.remote.responses.PokemonList
import com.example.pokedex.util.Resource
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val api: PokeApi
) {
    suspend fun getPokemonList(limit: Int, offset: Int): Resource<PokemonList> {
        val response = try {
            api.getPokemonList(limit, offset)
        } catch (e: Exception) {
            return Resource.Error("An unknown error occurred")
        }
        return Resource.Success(response)
    }

    suspend fun getPokemonInfo(pokemonName: String): Resource<Pokemon> {
        val response = try {
            api.getPokemonInfo(pokemonName)
        } catch (e: Exception) {
            return Resource.Error("An unknown error occurred")
        }
        return Resource.Success(response)
    }


}