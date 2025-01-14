import { Component, OnInit, Renderer2 } from '@angular/core';
import { CodeStoreService } from '../store/code-store.service';
import { ResponseDTO } from '../interface/responseDTO.interface';


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
  isDefaultColor: boolean = true;
  _lexicalErrorsPosition: number[][] = [];
  currentDragonImage: string = '../../assets/dragon.png'; // Imagem inicial
  // dragonImages: string[] = [
  //   '../../assets/dragon.png',
  //   '../../assets/dragon-1.png'
  // ];
  // currentIndex: number = 0; // Índice da imagem atual

  constructor(private codeStoreService: CodeStoreService, private renderer: Renderer2) {}

  ngOnInit(): void {
    this.toggleColors();
  }

  // toggleDragonImages(): void {
  //   setInterval(() => {
  //     
  //   }, 1000); // Alterna a cada 1 segundo
  // }

  private toggleColors(): void {
    setInterval(() => {
      if (this.screenMode === '') {
        const color = this.isDefaultColor ? '#16131C' : '#fe4a3c';
  
        // Pega as referências
        const btn1Elements = document.querySelectorAll('.btn-1');
        const lblBtn1Elements = document.querySelectorAll('.lbl-btn-1');
  
        // Alterna as cores
        btn1Elements.forEach((btn) => {
          this.renderer.setStyle(btn, 'background-color', color);
        });
        lblBtn1Elements.forEach((label) => {
          this.renderer.setStyle(label, 'color', color);
        });
  
        // Alterna o estado da cor
        this.isDefaultColor = !this.isDefaultColor;
      }
    }, 700);
  }

  escreverBtn(): void {
    this.screenMode = 'src'

    // Pega as referências
    const btn1Elements = document.querySelectorAll('.btn-1');
    const lblBtn1Elements = document.querySelectorAll('.lbl-btn-1');
    const lblSrc = document.getElementById("lbl-src");
    const lblOutput = document.querySelectorAll('.lbl-output');

    // Alterna as cores
    btn1Elements.forEach((btn) => {
      this.renderer.setStyle(btn, 'background-color', '#16131C');
    });
    lblBtn1Elements.forEach((label) => {
      this.renderer.setStyle(label, 'color', '#16131C');
    });
    this.renderer.setStyle(lblSrc, 'color', '#fe4a3c');
    lblOutput.forEach((label) => {
      this.renderer.setStyle(label, 'color', '#16131C');
    });
  }

  // Setter para alterar a propriedade e executar uma ação sempre que ela mudar
  set lexicalErrorsPosition(value: number[][]) {
    this._lexicalErrorsPosition = value;
    console.log('lexicalErrorsPosition foi alterado para:', value); // Mensagem no console
  }

  postCode(): void {
    if (this.screenMode == 'src') {
      this.codeStoreService.postCode(this.srcContent)
        .subscribe({
          next: (response) => {
            console.log('Código enviado com sucesso:', response);
            this.outputContent = response.map((dto: ResponseDTO) => dto.message).join('\n');
            this.lexicalErrorsPosition = response.map((dto: ResponseDTO) => [dto.line, dto.column]);
          },
          error: (error) => {
            console.error('Erro ao enviar o código:', error);
            alert('Erro ao enviar o código.');
          }
        });
    }
  }

  compilarBtn(): void {
    if (this.srcContent.trim()) {
      this.postCode();
      this.screenMode = 'output'
  
      // Pega as referências
      const btn1Elements = document.querySelectorAll('.btn-1');
      const lblBtn1Elements = document.querySelectorAll('.lbl-btn-1');
      const lblSrc = document.getElementById("lbl-src");
      const lblOutput = document.querySelectorAll('.lbl-output');
  
      // Alterna as cores
      btn1Elements.forEach((btn) => {
        this.renderer.setStyle(btn, 'background-color', '#16131C');
      });
      lblBtn1Elements.forEach((label) => {
        this.renderer.setStyle(label, 'color', '#16131C');
      });
      this.renderer.setStyle(lblSrc, 'color', '#16131C');
      lblOutput.forEach((label) => {
        this.renderer.setStyle(label, 'color', '#fe4a3c');
      });
    } else {
      alert('O campo de código está vazio.');
    }

    debugger;
  }
}