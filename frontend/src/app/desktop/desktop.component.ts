import { Component, OnInit, Renderer2 } from '@angular/core';
import { Token } from '../interface/token.interface';
import { CodeStoreService } from '../store/code-store.service';

@Component({
  selector: 'app-desktop',
  standalone: false,
  templateUrl: './desktop.component.html',
  styleUrls: ['./desktop.component.scss'],
})
export class DesktopComponent implements OnInit {
  screenMode: string = '';
  srcContent: string = '';
  outputContent: string = '';
  isDefaultColor: boolean = true;
  tokens: Token[] = [];
  currentDragonImage: string = '../../assets/dragon.png';

  constructor(
    private codeStoreService: CodeStoreService,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    this.toggleColors();
  }

  private toggleColors(): void {
    setInterval(() => {
      if (this.screenMode === '') {
        const color = this.isDefaultColor ? '#16131C' : '#fe4a3c';

        const btn1Elements = document.querySelectorAll('.btn-1');
        const lblBtn1Elements = document.querySelectorAll('.lbl-btn-1');

        btn1Elements.forEach((btn) => {
          this.renderer.setStyle(btn, 'background-color', color);
        });
        lblBtn1Elements.forEach((label) => {
          this.renderer.setStyle(label, 'color', color);
        });

        this.isDefaultColor = !this.isDefaultColor;
      }
    }, 700);
  }

  escreverBtn(): void {
    this.screenMode = 'src';
    this.outputContent = '';
    this.tokens = [];

    const btn1Elements = document.querySelectorAll('.btn-1');
    const lblBtn1Elements = document.querySelectorAll('.lbl-btn-1');
    const lblSrc = document.getElementById('lbl-src');
    const lblOutput = document.querySelectorAll('.lbl-output');

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
  // set lexicalErrorsPosition(value: number[][]) {
  //   this._lexicalErrorsPosition = value;
  //   console.log('lexicalErrorsPosition foi alterado para:', value); // Mensagem no console
  // }

  postCode(): void {
    if (this.screenMode == 'src') {
      this.codeStoreService.postCode(this.srcContent).subscribe({
        next: (response: Token[]) => {
          console.log('Código analisado com sucesso:', response);
          this.tokens = response;
          this.formatOutput();
        },
        error: (error) => {
          console.error('Erro ao analisar o código:', error);
          this.outputContent = 'Erro ao analisar o código.';
        },
      });
    }
  }

  private formatOutput(): void {
    this.outputContent = this.tokens
      .map(
        (token) =>
          `[${token.tipo}] "${token.lexema}" (linha: ${token.linha}, coluna: ${token.coluna})`
      )
      .join('\n');
  }

  compilarBtn(): void {
    if (this.srcContent.trim()) {
      this.postCode();
      this.screenMode = 'output';

      const btn1Elements = document.querySelectorAll('.btn-1');
      const lblBtn1Elements = document.querySelectorAll('.lbl-btn-1');
      const lblSrc = document.getElementById('lbl-src');
      const lblOutput = document.querySelectorAll('.lbl-output');

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
  }
}
