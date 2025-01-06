import { Component } from '@angular/core';
import { CodeStoreService } from '../store/code-store.service';  // Importe o serviço

@Component({
  selector: 'app-desktop',
  standalone: false,
  
  templateUrl: './desktop.component.html',
  styleUrl: './desktop.component.scss'
})
export class DesktopComponent {
  codeContent: string = '';

  constructor(private codeStoreService: CodeStoreService) {}

  postCode(): void {
    if (this.codeContent.trim()) {
      this.codeStoreService.postCode(this.codeContent)
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
}
