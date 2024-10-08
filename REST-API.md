To lazy to do it in openapi v3.
Default port is `15151`

## Endpoints

- `POST /add`
  - ```ts
      interface Request{
        body: {
          link: string;
          headers?: Record<string,string>
          downloadPage?: string
        }
      }
    ```
- `GET /queues`
  - Returns list of queues
    - ```ts
      interface Queue{
        id: number
        name: string
      }
      type Response = {
        body: Queue[]
      }
    ```
- `POST /add-download-task`
  - ```ts
      interface Request{
        body: {
          downloadSource: {
            link: string;
            headers?: Record<string,string>;
            downloadPage?: string;
          }
          folder?: string; //unix style path separator
          name?: string;
          folder?: string;
          queueId?: number;
        }
      }
    ```