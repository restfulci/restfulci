from python:3.9-alpine

RUN pip install flask

WORKDIR /code
COPY app.py /code/app.py

ENV FLASK_APP=app.py

EXPOSE 5000
CMD ["flask", "run", "--host=0.0.0.0"]
