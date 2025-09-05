FROM ollama/ollama

COPY ./entrypoint.sh /entrypoint.sh

WORKDIR /

RUN chmod +x entrypoint.sh

EXPOSE 11434