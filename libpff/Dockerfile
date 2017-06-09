FROM registry.swarm/libpff

RUN pip install jinja2 unicodecsv flask

WORKDIR /app
COPY ./ /app

CMD python -u run.py
