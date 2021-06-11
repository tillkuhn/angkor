import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {NGXLogger} from 'ngx-logger';


// declare var webkitSpeechRecognition: any;

// TODO: get this injected properly
const SpeechRecognition = window && (
  (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition ||
  (window as any).mozSpeechRecognition || (window as any).msSpeechRecogntion
);

/**
 * Speech Recognition Service ... inspired by
 *
 * - https://github.com/ladyleet/rxjs-test/blob/master/src/app/speech.service.ts
 * - https://github.com/kamiazya/ngx-speech-recognition
 * - https://codeburst.io/creating-a-speech-recognition-app-in-angular-8d1fd8d977ca
 */
@Injectable({
  providedIn: 'root'
})
export class SpeechService {

  constructor(private logger: NGXLogger) {}

  listen(): Observable<string[]> {
    return new Observable<string[]>(observer => {
      const speech = new SpeechRecognition();
      speech.lang = 'de-DE';

      const resultHandler = (e: any) => {
        console.log(e);
        const results: string[] = this.cleanSpeechResults(e.results);
        observer.next(results);
        observer.complete();
      };

      const errorHandler = (err) => {
        observer.error(err);
      };

      speech.addEventListener('result', resultHandler);
      speech.addEventListener('error', errorHandler);
      speech.start();

      return () => {
        speech.removeEventListener('result', resultHandler);
        speech.removeEventListener('error', errorHandler);
        speech.abort();
      };
    });
  }

  private cleanSpeechResults(results: any): string[] {
    return (Array.from(results) as string[])
      .reduce(
        (final: string[], result: any) =>
          final.concat(Array.from(result, (x: any) => x.transcript)),
        []
      );
  }
}
