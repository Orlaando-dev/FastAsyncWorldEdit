/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.world.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.registry.state.PropertyKey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A Fuzzy BlockState. Used for partial matching.
 *
 * Immutable, construct with {@link FuzzyBlockState.Builder}.
 */
public class FuzzyBlockState extends BlockState {

    private final Map<PropertyKey, Object> state;

    FuzzyBlockState(BlockType blockType) {
        this(blockType, null);
    }

    private FuzzyBlockState(BlockType blockType, Map<Property<?>, Object> values) {
        super(blockType, blockType.getDefaultState().getInternalId(), blockType.getDefaultState().getOrdinal());
        if (values == null || values.isEmpty()) {
            state = Collections.emptyMap();
        } else {
            state = new HashMap<>(values.size());
            for (Map.Entry<Property<?>, Object> entry : values.entrySet()) {
                state.put(entry.getKey().getKey(), entry.getValue());
            }
        }
    }

    /**
     * Gets a full BlockState from this fuzzy one, filling in
     * properties with default values where necessary.
     *
     * @return The full BlockState
     */
    public BlockState getFullState() {
        BlockState state = getBlockType().getDefaultState();
        for (Map.Entry<Property<?>, Object> entry : getStates().entrySet()) {
            @SuppressWarnings("unchecked")
            Property<Object> objKey = (Property<Object>) entry.getKey();
            state = state.with(objKey, entry.getValue());
        }
        return state;
    }

    @Override
    public boolean equalsFuzzy(BlockStateHolder<?> o) {
        if (!getBlockType().equals(o.getBlockType())) {
            return false;
        }
        if (!state.isEmpty()) {
            for (Map.Entry<PropertyKey, Object> entry : state.entrySet()) {
                if (!Objects.equals(o.getState(entry.getKey()), entry.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public BlockState toImmutableState() {
        return getFullState();
    }

    /**
     * Gets an instance of a builder.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for FuzzyBlockState
     */
    public static class Builder {
        private BlockType type;
        private Map<Property<?>, Object> values = new HashMap<>();

        /**
         * The type of the Fuzzy BlockState
         *
         * @param type The type
         * @return The builder, for chaining
         */
        public Builder type(BlockType type) {
            checkNotNull(type);
            this.type = type;
            return this;
        }

        /**
         * The type of the Fuzzy BlockState
         *
         * @param state The state
         * @return The builder, for chaining
         */
        public Builder type(BlockState state) {
            checkNotNull(state);
            this.type = state.getBlockType();
            return this;
        }

        /**
         * Adds a property to the fuzzy BlockState
         *
         * @param property The property
         * @param value The value
         * @param <V> The property type
         * @return The builder, for chaining
         */
        public <V> Builder withProperty(Property<V> property, V value) {
            checkNotNull(property);
            checkNotNull(value);
            checkNotNull(type, "The type must be set before the properties!");
            type.getProperty(property.getName()); // Verify the property is valid for this type
            values.put(property, value);
            return this;
        }

        /**
         * Builds a FuzzyBlockState from this builder.
         *
         * @return The fuzzy BlockState
         */
        public FuzzyBlockState build() {
            checkNotNull(type);
            if (values.isEmpty()) {
                return type.getFuzzyMatcher();
            }
            return new FuzzyBlockState(type, values);
        }

        /**
         * Resets the builder.
         *
         * @return The builder, for chaining
         */
        public Builder reset() {
            this.type = null;
            this.values.clear();
            return this;
        }
    }
}