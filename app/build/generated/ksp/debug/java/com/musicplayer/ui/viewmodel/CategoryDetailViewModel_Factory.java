package com.musicplayer.ui.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import com.musicplayer.data.repository.MediaRepository;
import com.musicplayer.player.PlaybackController;
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
public final class CategoryDetailViewModel_Factory implements Factory<CategoryDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<MediaRepository> mediaRepositoryProvider;

  private final Provider<PlaybackController> playbackControllerProvider;

  public CategoryDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<MediaRepository> mediaRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.mediaRepositoryProvider = mediaRepositoryProvider;
    this.playbackControllerProvider = playbackControllerProvider;
  }

  @Override
  public CategoryDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), mediaRepositoryProvider.get(), playbackControllerProvider.get());
  }

  public static CategoryDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<MediaRepository> mediaRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    return new CategoryDetailViewModel_Factory(savedStateHandleProvider, mediaRepositoryProvider, playbackControllerProvider);
  }

  public static CategoryDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      MediaRepository mediaRepository, PlaybackController playbackController) {
    return new CategoryDetailViewModel(savedStateHandle, mediaRepository, playbackController);
  }
}
