FROM golang:latest

WORKDIR /go/src/app
COPY ./beer-search /go/src/app/

RUN go-wrapper download
RUN go-wrapper install

CMD ["go-wrapper", "run"]

