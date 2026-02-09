package com.musicplayer.di;

import android.content.Context;
import com.musicplayer.player.PlaybackController;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvidePlaybackControllerFactory implements Factory<PlaybackController> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvidePlaybackControllerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PlaybackController get() {
    return providePlaybackController(contextProvider.get());
  }

  public static AppModule_ProvidePlaybackControllerFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvidePlaybackControllerFactory(contextProvider);
  }

  public static PlaybackController providePlaybackController(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePlaybackController(context));
  }
}
