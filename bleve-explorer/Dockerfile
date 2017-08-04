FROM golang:latest

WORKDIR /go/src/app
COPY ./ /go/src/app/

RUN go-wrapper download
RUN go-wrapper install

CMD ["go-wrapper", "run"]

