import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { routes } from './app.routes';
import { AppComponent } from './app.component';
import { ExemploComponent } from './modules/exemplo/exemplo.component';

@NgModule({
  //Colocar o declaration
  imports: [
    BrowserModule,
    RouterModule.forRoot(routes),
    AppComponent,
    ExemploComponent,
  ],
  providers: [],
})
export class AppModule {}
