import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import { HttpClientModule } from '@angular/common/http';
import { ExemploStore } from './app/stores/exemplo.store';

bootstrapApplication(AppComponent, {
  providers: [provideRouter(routes), HttpClientModule, ExemploStore],
}).catch((err) => console.error(err));
