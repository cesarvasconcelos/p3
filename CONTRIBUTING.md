# Guia de Contribuição

Olá! Seja bem-vindo(a) ao repositório da disciplina. Este arquivo explica como você pode contribuir com melhorias nos códigos que desenvolvemos juntos durante as aulas. Sua participação é muito valorizada e faz parte do aprendizado!

---

## Por que contribuir?

Durante as aulas, é natural que alguns trechos de código contenham pequenos erros, simplificações didáticas ou oportunidades de melhoria. Se você identificou algo assim, **ótimo — isso significa que você está aprendendo de verdade!**

Contribuir com correções ou melhorias é uma excelente forma de:

- Praticar o fluxo de trabalho profissional com Git e GitHub
- Desenvolver senso crítico sobre qualidade de código
- Ajudar seus colegas que também utilizam o repositório
- Ganhar experiência com colaboração em projetos reais

---

## Fluxo de Contribuição

Siga os passos abaixo para contribuir de forma organizada:

### 1. Verifique se o problema já foi reportado

Antes de abrir uma issue ou pull request, acesse a aba __Issues__ e verifique se alguém já reportou o mesmo problema. Se sim, você pode complementar a discussão existente com um comentário.

### 2. Abra uma Issue descrevendo o problema

Se o problema ainda não foi reportado, abra uma nova issue com:

- Um **título claro e objetivo** (ex: `Erro de indentação no exemplo da aula 03`)
- Uma **descrição do problema**: o que está errado ou o que poderia melhorar
- O **trecho de código** relevante (use blocos de código com três backticks)
- Se possível, uma **sugestão para o professor de como corrigir**

Não se preocupe em ser formal demais — pode escrever de forma simples e direta. O importante é ser claro.

### 3. Aguarde um retorno (ou sinalize que vai abrir um PR)

Após abrir a issue, aguarde um breve retorno confirmando que a correção é pertinente. Se quiser agilizar, você mesmo pode comentar na issue: *"Posso abrir um pull request com a correção?"*

Para erros simples e óbvios (como um typo ou uma linha faltando), você pode pular direto para o pull request — não precisa abrir uma issue.

### 4. Faça um fork e crie um branch

```bash
# Clone seu fork localmente
git clone https://github.com/SEU_USUARIO/NOME_DO_REPO.git

# Crie um branch com um nome descritivo
git checkout -b fix/erro-loop-aula-05
```

Use prefixos como:
- `fix/` para correções de erros
- `improve/` para melhorias de clareza ou boas práticas
- `docs/` para correções em comentários ou documentação

### 5. Faça a correção e envie

```bash
git add .
git commit -m "fix: corrige erro de índice no exemplo de lista da aula 05"
git push origin fix/erro-loop-aula-05
```

### 6. Abra um Pull Request

Acesse o repositório original e abra um Pull Request a partir do seu fork. No PR, inclua:

- Uma **descrição clara** do que foi alterado e por quê
- A referência à issue relacionada, se houver (ex: `Resolve #12`)
- O **trecho antes e depois** da correção, se ajudar a entender a mudança

---

## Boas práticas

- **Mantenha o escopo pequeno.** Cada PR deve resolver um problema de cada vez.
- **Não altere o estilo geral do código** sem discussão prévia na issue. O objetivo é preservar a linguagem didática usada em aula.
- **Seja respeitoso(a) nos comentários.** Este é um espaço de aprendizado coletivo.
- **Escreva mensagens de commit claras.** Use o padrão `tipo: descrição curta` (ex: `fix: remove variável não utilizada`).

---

## Dúvidas?

Se tiver qualquer dúvida sobre o processo, abra uma issue com o prefixo `[dúvida]` no título ou entre em contato durante a aula. Estou aqui para ajudar!

E obrigado por contribuir! Cada melhoria, por menor que seja, torna o material mais rico para todos.
