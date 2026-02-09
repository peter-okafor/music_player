package com.musicplayer.ui.viewmodel;

import android.content.Context;
import com.musicplayer.data.repository.MediaRepository;
import com.musicplayer.player.PlaybackController;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<MediaRepository> mediaRepositoryProvider;

  private final Provider<PlaybackController> playbackControllerProvider;

  public HomeViewModel_Factory(Provider<Context> contextProvider,
      Provider<MediaRepository> mediaRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    this.contextProvider = contextProvider;
    this.mediaRepositoryProvider = mediaRepositoryProvider;
    this.playbackControllerProvider = playbackControllerProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(contextProvider.get(), mediaRepositoryProvider.get(), playbackControllerProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<Context> contextProvider,
      Provider<MediaRepository> mediaRepositoryProvider,
      Provider<PlaybackController> playbackControllerProvider) {
    return new HomeViewModel_Factory(contextProvider, mediaRepositoryProvider, playbackControllerProvider);
  }

  public static HomeViewModel newInstance(Context context, MediaRepository mediaRepository,
      PlaybackController playbackController) {
    return new HomeViewModel(context, mediaRepository, playbackController);
  }
}
