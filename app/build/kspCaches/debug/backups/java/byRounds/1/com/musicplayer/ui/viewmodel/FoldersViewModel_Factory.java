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
public final class FoldersViewModel_Factory implements Factory<FoldersViewModel> {
  private final Provider<MediaRepository> mediaRepositoryProvider;

  public FoldersViewModel_Factory(Provider<MediaRepository> mediaRepositoryProvider) {
    this.mediaRepositoryProvider = mediaRepositoryProvider;
  }

  @Override
  public FoldersViewModel get() {
    return newInstance(mediaRepositoryProvider.get());
  }

  public static FoldersViewModel_Factory create(Provider<MediaRepository> mediaRepositoryProvider) {
    return new FoldersViewModel_Factory(mediaRepositoryProvider);
  }

  public static FoldersViewModel newInstance(MediaRepository mediaRepository) {
    return new FoldersViewModel(mediaRepository);
  }
}
