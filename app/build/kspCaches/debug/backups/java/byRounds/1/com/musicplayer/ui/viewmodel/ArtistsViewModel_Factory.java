package com.musicplayer.ui.viewmodel;

import com.musicplayer.data.repository.MediaRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ArtistsViewModel_Factory implements Factory<ArtistsViewModel> {
  private final Provider<MediaRepository> mediaRepositoryProvider;

  public ArtistsViewModel_Factory(Provider<MediaRepository> mediaRepositoryProvider) {
    this.mediaRepositoryProvider = mediaRepositoryProvider;
  }

  @Override
  public ArtistsViewModel get() {
    return newInstance(mediaRepositoryProvider.get());
  }

  public static ArtistsViewModel_Factory create(Provider<MediaRepository> mediaRepositoryProvider) {
    return new ArtistsViewModel_Factory(mediaRepositoryProvider);
  }

  public static ArtistsViewModel newInstance(MediaRepository mediaRepository) {
    return new ArtistsViewModel(mediaRepository);
  }
}
