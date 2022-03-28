/**
 * This is just a scratch file to execute plan typescript from the shell
 *
 * yarn global add ts-node
 * ts-node scratch.ts
 *
 * Declare Classes, enums and other types first, then scroll down and add any "main" code you want to execute
 */

// https://github.com/TypeStrong/ts-node/issues/922#issuecomment-673155000 edit tsconig to use import !
import {formatDistance, parseISO} from 'date-fns';

// Constant
// The declare keyword in typescript is useful for telling the typescript compiler that a declaration is defined somewhere else
// (somewhere written in an external javascript file or part of the runtime environment).
export declare const enum SomeState {
  OPEN = 0,
  CLOSED = 2
}

// String Literal Types and Union Types
// You can  write a function that expects a parameter of type EntityTypePath and have the TypeScript compiler check for you that only
// the values "places" or "notes"
export declare type EntityTypePath = 'places' | 'notes';

// String enums (avoid Heterogeneous enums!!!()
// ... have some subtle runtime diff that each member has to be constant-initialized with  string literal, or another string enum member.
export enum ListType {
  NoteStatus = 'note_status',
  AuthScopes = 'auth_scopes'
}

// Numeric Enums
enum Direction {
  Up = 1,
  Down // assumed to bt 2 and so forth
 }

// Interfaces
interface Place {
  name: string;
  country?: string;
}


// Class
export class Service {
  constructor() {
    console.log('hossa');
  }

  // Generic Function https://www.typescriptlang.org/docs/handbook/generics.html
  getItem<T>(): T {
    const a: any = {name: 'Hintertupfingen'} ;
    return a;
  }

  // https://www.vinta.com.br/blog/2015/javascript-lambda-and-arrow-functions/
  // (Fat) Arrow aka lambda Functions
  private adder  = (a, b) => a + b; // same as adder = function (a, b) { return a + b };
  evener = value => value % 2 === 0;
// this looks pretty nice when you change something like:
  fatArrow() {
    const numbers = [1, 2, 3, 4, 5];
    console.log(numbers.filter(this.evener));
  }

  filterNumbers(numbers: number[], predicate: (n: number) => boolean): number[] {
    return numbers.filter(predicate);
  }

}

// Class with static functions and nice documentation
export class Utils {

  /**
   * Format bytes as human-readable text.
   *
   * Credits: https://stackoverflow.com/questions/10420352/converting-file-size-in-bytes-to-human-readable-string/10420404
   *
   * @param bytes Number of bytes.
   * @param si True to use metric (SI, International System of Units) units, aka powers of 1000.
   * @param dp Number of decimal places to display.
   *
   * @return Formatted string. or a number.
   */
  static humanFileSize(bytes: number, si = false, dp = 1): string | number {
    return `${bytes} MB`;
  }

}

// Put things you want to test in above classes, start executing stuff here ...

const svc = new Service();
console.log('parseIso', formatDistance( parseISO('2021-02-01T14:20:23Z'), new Date()));
console.log('humanFileSize', Utils.humanFileSize(13));
svc.fatArrow();
console.log(svc.filterNumbers([11, 24, 35, 45, 66, 100], value => value % 2 === 0));

//
// Fun with enums see https://stackoverflow.com/a/17381004/4292075
//

// ListType.NoteStatus = 'note_status',
let noteStatEnum = ListType.NoteStatus;
let noteStatStr: string =  ListType.NoteStatus.toString(); // also returns note_status (i.e. enum Value)
let lookupStr = "NoteStatus"
let noteStatEnum2 = ListType[lookupStr]; // uses the key String, not the value to lookup enum
// todo https://www.codegrepper.com/code-examples/typescript/typescript+get+name+of+enum+value
let noteStatStr2 = ListType[ListType.NoteStatus]; // uses the key String, not the value to lookup enum DOES NOT WORK

console.log('Enum ListType.NoteStatus=',noteStatEnum.toUpperCase(), 'str',noteStatStr,'equals', noteStatEnum === ListType.NoteStatus,'hase2',noteStatEnum2,"str2",noteStatStr2); // prints note_status
// https://www.educba.com/typescript-key-value-pair/
// https://www.typescriptlang.org/play
interface Viech {
  id: number,
  name: string
}

let indexedViech: {[key: string]: Viech} = {
  hase: {id: 1,name: 'Hasenbaer'},
  katze: {id: 2,name: 'Katzi'},
}

console.log(indexedViech['hase']); // {  "id": 1,"name": "Hasenbaer"}
console.log(indexedViech['katze'].id); // 1
console.log(indexedViech['hund']); // undefined

// https://stackoverflow.com/a/40055555/4292075
Object.entries(indexedViech).forEach(
  ([key, value]) => console.log('entry',key, 'is named', value.name)
);
let viechArray = Object.values(indexedViech);
console.log(viechArray)
