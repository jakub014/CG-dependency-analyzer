/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.fasten.core.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * Each type is a class or an interface.
 */
public class JavaType {

	/** The FASTEN URI of this Java type. */
	private final String uri;

    /**
     * The source file name of this type.
     */
    private final String sourceFileName;

    /**
     * Methods of this type and their unique ids (unique within the same artifact).
     */
    private final Int2ObjectMap<JavaNode> methods;
    private final Object2IntMap<JavaNode> javaNodes;

    /**
     * Classes that this type inherits from in the order of instantiation.
     */
    private final LinkedList<FastenURI> superClasses;
    private final Map<String, JavaNode> definedMethods;

    public Map<String, JavaNode> getDefinedMethods() {
        return definedMethods;
    }

    /**
     * Interfaces that this type or its super classes implement.
     */
    private final List<FastenURI> superInterfaces;

    /**
     * Access modifier of this JavaType.
     */
    private final String access;

    /**
     * Flag indicating if this JavaType is final.
     */
    private final boolean isFinal;

    /**
     * Creates {@link JavaType} for the given data.
     *  @param sourceFile      the name of this type's source file
     * @param methods         a map of methods in this type with their indexed by their ids.
     * @param defineds        a map of all defined methods of a type to their signature.
     * @param superClasses    classes that this type extends.
     * @param superInterfaces interfaces that this type implements.
     * @param access          access modifier
     * @param isFinal         true if the Type is final
     */
    public JavaType(final String uri, final String sourceFile, final Int2ObjectMap<JavaNode> methods,
                    final Map<String, JavaNode> defineds,
                    final LinkedList<FastenURI> superClasses,
                    final List<FastenURI> superInterfaces, final String access,
                    final boolean isFinal) {
        this.uri = uri;
        this.sourceFileName = sourceFile;
        this.methods = methods;
        this.javaNodes = new Object2IntOpenHashMap<>();
        methods.forEach((x, y) -> javaNodes.put(y, x));;
        javaNodes.defaultReturnValue(-1);
        this.definedMethods = defineds;
        this.superClasses = superClasses;
        this.superInterfaces = superInterfaces;
        this.access = access;
        this.isFinal = isFinal;
    }

    /**
     * Creates {@link JavaType} for the given JSONObject.
     *
     * @param type JSONObject of a type including its source file name, map of methods, super
     *             classes and super interfaces.
     */
    public JavaType(final String uri, final JSONObject type) {
        this.uri = uri;
        this.sourceFileName = type.getString("sourceFile");

        final var methodsJson = type.getJSONObject("methods");
        this.methods = new Int2ObjectOpenHashMap<>();
        this.javaNodes = new Object2IntOpenHashMap<>();
        javaNodes.defaultReturnValue(-1);
        this.definedMethods = new HashMap<>();
        for (final var methodKey : methodsJson.keySet()) {
            final var nodeJson = methodsJson.getJSONObject(methodKey);

            final var metadata = nodeJson.getJSONObject("metadata");
            final var node = new JavaNode(FastenURI.create(nodeJson.getString("uri")), metadata.toMap());
            final int k = Integer.parseInt(methodKey);
			this.methods.put(k, node);
            this.javaNodes.put(node, k);
            if (!metadata.isEmpty()) {
                if (metadata.getBoolean("defined")){
                    definedMethods.put(node.getSignature(), node);
                }
            }
        }

        final var superClassesJSON = type.getJSONArray("superClasses");
        this.superClasses = new LinkedList<>();
        final int numberOfSuperClasses = superClassesJSON.length();
        for (int i = 0; i < numberOfSuperClasses; i++) {
            this.superClasses.add(FastenURI.create(superClassesJSON.getString(i)));
        }

        final var superInterfacesJSON = type.getJSONArray("superInterfaces");
        this.superInterfaces = new ArrayList<>();
        final int numberOfSuperInterfaces = superInterfacesJSON.length();
        for (int i = 0; i < numberOfSuperInterfaces; i++) {
            this.superInterfaces.add(FastenURI.create(superInterfacesJSON.getString(i)));
        }
        this.access = type.getString("access");
        this.isFinal = type.getBoolean("final");
    }

    public String getUri() {
        return uri;
    }
    
    public String getSourceFileName() {
        return sourceFileName;
    }

    public Int2ObjectMap<JavaNode> getMethods() {
        return this.methods;
    }

    public LinkedList<FastenURI> getSuperClasses() {
        return superClasses;
    }

    public List<FastenURI> getSuperInterfaces() {
        return superInterfaces;
    }

    public String getAccess() {
        return access;
    }

    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Returns the integer associated to the given JavaNode.
     *
     * @param node a {@link JavaNode}.
     * @return the associated integer key, or &minus;1 if {@code node} is not in the map.
     */
    public int getMethodKey(final JavaNode node) {
        return javaNodes.getInt(node);
    }
    
    /**
     * Puts a JavaNode to the list of methods of this {@link JavaType}.
     *
     * @param node new node to add
     * @param key  the key corresponding to this JavaNode
     */
    public void addMethod(final JavaNode node, final int key) {
        methods.put(key, node);
        javaNodes.put(node, key);
    }

    /**
     * Converts all the values of a given Map to String.
     *
     * @param map map of id-s and corresponding JavaNodes
     */
    public static Int2ObjectMap<JSONObject> toMapOfString(final Int2ObjectMap<JavaNode> map) {
        final Int2ObjectMap<JSONObject> methods = new Int2ObjectOpenHashMap<>();
        for (final var entry : map.entrySet()) {
            final JSONObject node = new JSONObject();
            node.put("uri", entry.getValue().getUri());
            node.put("metadata", new JSONObject(entry.getValue().getMetadata()));
            methods.put(entry.getKey(), node);
        }
        return methods;
    }

    /**
     * Converts elements of a given list to String.
     *
     * @param list a list of elements to be converted
     */
    public static List<String> toListOfString(final List<?> list) {
        final List<String> result = new ArrayList<>();
        for (final var fastenURI : list) {
            result.add(fastenURI.toString());
        }
        return result;
    }

    /**
     * Get all defined methods.
     *
     * @param signature method signature
     * @return optional map of all defined methods
     */
    public Optional<Map.Entry<Integer, JavaNode>> getDefined(String signature) {
        return methods.entrySet()
                .stream()
                .filter(node -> (Boolean) node.getValue().metadata.get("defined"))
                .filter(node -> node.getValue().getSignature().equals(signature))
                .findAny();
    }

    /**
     * Converts this {@link JavaType} object to its JSON representation.
     *
     * @return the corresponding JSON representation.
     */
    public JSONObject toJSON() {
        final var result = new JSONObject();

        result.put("methods", toMapOfString(this.methods));
        result.put("superClasses", toListOfString(this.superClasses));
        result.put("superInterfaces", toListOfString(this.superInterfaces));
        result.put("sourceFile", this.sourceFileName);
        result.put("access", this.access);
        result.put("final", this.isFinal);

        return result;
    }

    @Override
    public String toString() {
        return "JavaType{"
                + "sourceFileName='" + sourceFileName + '\''
                + ", methods=" + methods
                + ", superClasses=" + superClasses
                + ", superInterfaces=" + superInterfaces
                + ", access=" + access
                + ", final=" + isFinal
                + '}';
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((access == null) ? 0 : access.hashCode());
		result = prime * result + ((definedMethods == null) ? 0 : definedMethods.hashCode());
		result = prime * result + (isFinal ? 1231 : 1237);
		result = prime * result + ((javaNodes == null) ? 0 : javaNodes.hashCode());
		result = prime * result + ((methods == null) ? 0 : methods.hashCode());
		result = prime * result + ((sourceFileName == null) ? 0 : sourceFileName.hashCode());
		result = prime * result + ((superClasses == null) ? 0 : superClasses.hashCode());
		result = prime * result + ((superInterfaces == null) ? 0 : superInterfaces.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		JavaType other = (JavaType)obj;
		if (access == null) {
			if (other.access != null) return false;
		} else if (!access.equals(other.access)) return false;
		if (definedMethods == null) {
			if (other.definedMethods != null) return false;
		} else if (!definedMethods.equals(other.definedMethods)) return false;
		if (isFinal != other.isFinal) return false;
		if (javaNodes == null) {
			if (other.javaNodes != null) return false;
		} else if (!javaNodes.equals(other.javaNodes)) return false;
		if (methods == null) {
			if (other.methods != null) return false;
		} else if (!methods.equals(other.methods)) return false;
		if (sourceFileName == null) {
			if (other.sourceFileName != null) return false;
		} else if (!sourceFileName.equals(other.sourceFileName)) return false;
		if (superClasses == null) {
			if (other.superClasses != null) return false;
		} else if (!superClasses.equals(other.superClasses)) return false;
		if (superInterfaces == null) {
			if (other.superInterfaces != null) return false;
		} else if (!superInterfaces.equals(other.superInterfaces)) return false;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		return true;
	}

    
}
