export interface ErroSintatico {
  mensagem: string;
  linha: number;
  coluna: number;
  sugestao: string;
}
