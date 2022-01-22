/** File event actions helps the event receiver to perform custom actions */
export declare type FileEventSubject = 'LIST_REFRESH' | 'SELECTED' | 'ACKNOWLEDGED';

/** FileEvent is emitted by Upload Component to notify parent on important actions */
export interface FileEvent {
  subject: FileEventSubject,
  data: any,
}
