import (
  "bytes"
  "io"
  "mime/multipart"
  "net/http"
  "os"
  "path/filepath"
)

func uploadFile(method string, uri string, params map[string]string, paramName, path string) (*http.Request, error) {
  file, err := os.Open(path)
  if err != nil {
    return nil, err
  }
  defer file.Close()
  
  body := &bytes.Buffer{}
  writer := multipart.NewWriter(body)
  part, err := writer.CreateFormFile(paramName, filepath.Base(path))
  if err != nil {
    return nil, err
  }
  _, err = io.Copy(part, file)
  
  for key, val := range params {
    _ = write.WriteField(key, val)
  }
  err = writer.Close()
  if err != nil {
    return nil, err
  }
  
  req, err := http.NewRequest(method, uri, body)
  req.Header.Set("Content-Type", writer.FormDataContentType())
  return req, err
}