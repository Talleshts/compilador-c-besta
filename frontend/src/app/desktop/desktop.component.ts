import { Component, OnInit } from '@angular/core';
import { CodeStoreService } from '../store/code-store.service';

@Component({
  selector: 'app-desktop',
  standalone: false,
  templateUrl: './desktop.component.html',
  styleUrls: ['./desktop.component.scss']
})
export class DesktopComponent implements OnInit {
  screenMode: string = ''

  srcContent: string = '';
  outputContent: string = '';

  currentDragonImage: string = '../../assets/dragon.png'; // Imagem inicial
  dragonImages: string[] = [
    '../../assets/dragon.png',
    '../../assets/dragon-1.png'
  ];
  currentIndex: number = 0; // Índice da imagem atual

  constructor(private codeStoreService: CodeStoreService) {}

  ngOnInit(): void {
    this.toggleDragonImages();
  }

  toggleDragonImages(): void {
    // setInterval(() => {
    //   
    // }, 1000); // Alterna a cada 1 segundo
  }

  escreverBtn(): void {
    this.screenMode = 'src'
  }

  postCode(): void {
    if (this.srcContent.trim()) {
      this.codeStoreService.postCode(this.srcContent)
        .subscribe({
          next: (response) => {
            console.log('Código enviado com sucesso:', response);
            alert('Código enviado com sucesso!');
          },
          error: (error) => {
            console.error('Erro ao enviar o código:', error);
            alert('Erro ao enviar o código.');
          }
        });
    } else {
      alert('O campo de código está vazio.');
    }
  }

  compilarBtn(): void {
    this.screenMode = 'output'
  }
}