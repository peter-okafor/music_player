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
public final class AlbumsViewModel_Factory implements Factory<AlbumsViewModel> {
  private final Provider<MediaRepository> mediaRepositoryProvider;

  public AlbumsViewModel_Factory(Provider<MediaRepository> mediaRepositoryProvider) {
    this.mediaRepositoryProvider = mediaRepositoryProvider;
  }

  @Override
  public AlbumsViewModel get() {
    return newInstance(mediaRepositoryProvider.get());
  }

  public static AlbumsViewModel_Factory create(Provider<MediaRepository> mediaRepositoryProvider) {
    return new AlbumsViewModel_Factory(mediaRepositoryProvider);
  }

  public static AlbumsViewModel newInstance(MediaRepository mediaRepository) {
    return new AlbumsViewModel(mediaRepository);
  }
}
