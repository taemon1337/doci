from os.path import join
from pst_indexer import main
from flask import Flask, request

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = '/tmp/'
app.config['ALLOWED_EXTENSIONS'] = set(['pst'])

def allowed_file(filename):
  return '.' in filename and filename.rsplit('.', 1)[1] in app.config['ALLOWED_EXTENSIONS']

@app.route('/upload', methods=['POST'])
def upload():
  print('UPLOADING')
  file = request.files['file']

  if file and allowed_file(file.filename):
    filepath = join(app.config['UPLOAD_FOLDER'], file.filename) 
    file.save(filepath)
    return main(filepath)
  else:
    return Error('Invalid Request!')

if __name__ == '__main__':
  app.run(host="0.0.0.0", port=int("8080"), debug=True)
