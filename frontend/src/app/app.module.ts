import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http'; // Importar HttpClientModule
import { routes } from './app.routes';
import { AppComponent } from './app.component';
import { ExemploComponent } from './modules/exemplo/exemplo.component';
import { ExemploStore } from './stores/exemplo.store'; // Importar ExemploStore

@NgModule({
  declarations: [AppComponent, ExemploComponent],
  imports: [
    BrowserModule,
    RouterModule.forRoot(routes),
    HttpClientModule, // Adicionar HttpClientModule
  ],
  providers: [ExemploStore], // Adicionar ExemploStore aos providers
})
export class AppModule {}
