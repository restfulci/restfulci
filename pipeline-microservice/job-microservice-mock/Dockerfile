from python:3.8.3-alpine

RUN pip install flask

WORKDIR /code
COPY app.py /code/app.py

ENV FLASK_APP=app.py

EXPOSE 5000
CMD ["flask", "run", "--host=0.0.0.0"]
