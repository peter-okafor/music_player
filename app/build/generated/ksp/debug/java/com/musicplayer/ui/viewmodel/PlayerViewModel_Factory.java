package com.musicplayer.ui.viewmodel;

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
public final class PlayerViewModel_Factory implements Factory<PlayerViewModel> {
  private final Provider<PlaybackController> playbackControllerProvider;

  public PlayerViewModel_Factory(Provider<PlaybackController> playbackControllerProvider) {
    this.playbackControllerProvider = playbackControllerProvider;
  }

  @Override
  public PlayerViewModel get() {
    return newInstance(playbackControllerProvider.get());
  }

  public static PlayerViewModel_Factory create(
      Provider<PlaybackController> playbackControllerProvider) {
    return new PlayerViewModel_Factory(playbackControllerProvider);
  }

  public static PlayerViewModel newInstance(PlaybackController playbackController) {
    return new PlayerViewModel(playbackController);
  }
}
