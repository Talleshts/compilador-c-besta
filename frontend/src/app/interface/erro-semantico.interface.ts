export interface ErroSemantico {
    mensagem: string;
    linha: number;
    coluna: number;
    sugestao: string;
    tipo: string;
} 