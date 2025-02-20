import { Component, OnInit, Renderer2 } from '@angular/core';
import { ErroSintatico } from '../interface/erro-sintatico.interface';
import { ErroSemantico } from '../interface/erro-semantico.interface';
import { CustomToken } from '../interface/token.interface';
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
  code: string = '';
  isDefaultColor: boolean = true;
  tokens: CustomToken[] = [];
  errosSintaticos: ErroSintatico[] = [];
  errosSemanticos: ErroSemantico[] = [];
  currentDragonImage: string = '../../assets/dragon.png';

  constructor(
    private codeStoreService: CodeStoreService,
    private renderer: Renderer2
  ) { }

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
    this.errosSintaticos = [];
    this.errosSemanticos = [];

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

  postCode(): void {
    if (this.screenMode == 'src') {
      this.codeStoreService.analyzeLexica(this.srcContent).subscribe({
        next: (response: any) => {
          console.log('Código analisado com sucesso:', response);
          this.tokens = response.tokens || [];
          this.formatOutput();
        },
        error: (error) => {
          console.error('Erro ao analisar o código:', error);
          this.outputContent = 'Erro ao analisar o código.';
        },
      });
    }
  }

  postCodeSintatico(): void {
    if (this.screenMode == 'src') {
      this.codeStoreService.analyzeSintatica(this.srcContent).subscribe({
        next: (response: any) => {
          console.log('Análise sintática realizada com sucesso:', response);
          this.errosSintaticos = response.errosSintaticos || [];
          this.formatOutputSintatico();
        },
        error: (error) => {
          console.error('Erro ao realizar análise sintática:', error);
          this.outputContent = 'Erro ao realizar análise sintática.';
        },
      });
    }
  }

  postCodeSemantico(): void {
    if (this.screenMode == 'src') {
      this.codeStoreService.analyzeSemantica(this.srcContent).subscribe({
        next: (response: any) => {
          console.log('Análise semântica realizada com sucesso:', response);
          this.errosSemanticos = response.errosSemanticos || [];
          this.formatOutputSemantico();
        },
        error: (error) => {
          console.error('Erro ao realizar análise semântica:', error);
          this.outputContent = 'Erro ao realizar análise semântica.';
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

  private formatOutputSintatico(): void {
    this.outputContent = this.errosSintaticos
      .map(
        (erro) =>
          `Erro: ${erro.mensagem} (linha: ${erro.linha}, coluna: ${erro.coluna})\nSugestão: ${erro.sugestao}`
      )
      .join('\n');
  }

  private formatOutputSemantico(): void {
    this.outputContent = this.errosSemanticos
      .map(
        (erro) =>
          `Erro ${erro.tipo}: ${erro.mensagem} (linha: ${erro.linha}, coluna: ${erro.coluna})\nSugestão: ${erro.sugestao}`
      )
      .join('\n');
  }

  compilarLexicoBtn(): void {
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

  compilarSintaticoBtn(): void {
    if (this.srcContent.trim()) {
      this.postCodeSintatico();
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

  compilarSemanticoBtn(): void {
    if (this.srcContent.trim()) {
      this.postCodeSemantico();
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
