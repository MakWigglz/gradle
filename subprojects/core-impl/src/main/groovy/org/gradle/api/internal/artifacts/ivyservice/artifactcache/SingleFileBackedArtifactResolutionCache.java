/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.ivyservice.artifactcache;

import org.apache.ivy.core.module.id.ArtifactRevisionId;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.gradle.api.internal.artifacts.ivyservice.ArtifactCacheMetaData;
import org.gradle.api.internal.artifacts.ivyservice.CacheLockingManager;
import org.gradle.cache.DefaultSerializer;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.internal.FileLock;
import org.gradle.cache.internal.btree.BTreePersistentIndexedCache;
import org.gradle.util.TimeProvider;
import org.jfrog.wharf.ivy.model.WharfResolverMetadata;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SingleFileBackedArtifactResolutionCache implements ArtifactResolutionCache {
    private final TimeProvider timeProvider;
    private final ArtifactCacheMetaData cacheMetadata;
    private final CacheLockingManager cacheLockingManager;
    private PersistentIndexedCache<RevisionKey, ArtifactResolutionCacheEntry> cache;

    public SingleFileBackedArtifactResolutionCache(ArtifactCacheMetaData cacheMetadata, TimeProvider timeProvider, CacheLockingManager cacheLockingManager) {
        this.timeProvider = timeProvider;
        this.cacheLockingManager = cacheLockingManager;
        this.cacheMetadata = cacheMetadata;
    }
    
    private PersistentIndexedCache<RevisionKey, ArtifactResolutionCacheEntry> getCache() {
        if (cache == null) {
            cache = initCache();
        }
        return cache;
    }

    private PersistentIndexedCache<RevisionKey, ArtifactResolutionCacheEntry> initCache() {
        File artifactResolutionCacheFile = new File(cacheMetadata.getCacheDir(), "artifact-resolution.bin");
        FileLock artifactResolutionCacheLock = cacheLockingManager.getCacheMetadataFileLock(artifactResolutionCacheFile);
        return new BTreePersistentIndexedCache<RevisionKey, ArtifactResolutionCacheEntry>(artifactResolutionCacheFile, artifactResolutionCacheLock,
                new DefaultSerializer<ArtifactResolutionCacheEntry>(ArtifactResolutionCacheEntry.class.getClassLoader()));
    }

    public CachedArtifactResolution getCachedArtifactResolution(DependencyResolver resolver, ArtifactRevisionId artifactId) {
         ArtifactResolutionCacheEntry artifactResolutionCacheEntry = getCache().get(createKey(resolver, artifactId));
        if (artifactResolutionCacheEntry == null) {
            return null;
        }
        return new DefaultCachedArtifactResolution(artifactId, artifactResolutionCacheEntry, timeProvider);
    }

    public void recordArtifactResolution(DependencyResolver resolver, ArtifactRevisionId artifactId, File artifactFile) {
        getCache().put(createKey(resolver, artifactId), createEntry(artifactFile));
    }

    private RevisionKey createKey(DependencyResolver resolver, ArtifactRevisionId artifactId) {
        return new RevisionKey(resolver, artifactId);
    }

    private ArtifactResolutionCacheEntry createEntry(File artifactFile) {
        return new ArtifactResolutionCacheEntry(artifactFile, timeProvider);
    }

    private static class RevisionKey implements Serializable {
        private final String resolverId;
        private final String artifactId;
        private final Map<String, String> extraAttributes;

        private RevisionKey(DependencyResolver resolver, ArtifactRevisionId revision) {
            this.resolverId = new WharfResolverMetadata(resolver).getId();
            this.artifactId = revision.toString();
            this.extraAttributes = new HashMap<String, String>(revision.getQualifiedExtraAttributes());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof RevisionKey)) {
                return false;
            }
            RevisionKey other = (RevisionKey) o;
            return resolverId.equals(other.resolverId) && artifactId.equals(other.artifactId) && extraAttributes.equals(other.extraAttributes);
        }

        @Override
        public int hashCode() {
            return resolverId.hashCode() ^ artifactId.hashCode() ^ extraAttributes.hashCode();
        }
    }

}
