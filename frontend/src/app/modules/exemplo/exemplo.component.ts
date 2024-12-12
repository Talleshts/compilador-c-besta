import { Component, OnInit } from '@angular/core';
import { ExemploStore } from '../../stores/exemplo.store';

@Component({
  selector: 'app-exemplo',
  imports: [],
  templateUrl: './exemplo.component.html',
  styleUrl: './exemplo.component.css',
})
export class ExemploComponent implements OnInit {
  title = 'frontend';
  message: string = '';

  constructor(private exemploStore: ExemploStore) {}

  ngOnInit(): void {
    this.exemploStore.getHello().subscribe((data: string) => {
      this.message = data;
    });
  }
}
