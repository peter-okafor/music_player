package com.musicplayer.di;

import android.content.Context;
import com.musicplayer.data.repository.MediaRepository;
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
public final class AppModule_ProvideMediaRepositoryFactory implements Factory<MediaRepository> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideMediaRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MediaRepository get() {
    return provideMediaRepository(contextProvider.get());
  }

  public static AppModule_ProvideMediaRepositoryFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideMediaRepositoryFactory(contextProvider);
  }

  public static MediaRepository provideMediaRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMediaRepository(context));
  }
}
