// Example:
// {
//   "filename": "20201015_132245_autobahn_mini.jpg",
//   "presignedUrl": "https://timafe-angkor-data-dev.s3.eu-central-1.amazonaws.com/appdata/places/12345...."
//   "tags": {
//   "fx": "Woof",
//     "horst": "klaus"
// }

export interface FileItem {
  filename: string;
  presignedUrl?: string;
  tags?: any;
}

/**
 * File Upload Request for Imagine
 */
export interface FileUpload {
  entityType: string;
  entityId: string;
  url: string;

  /**
   * Filename is optional, if not present the name will be derived from the URL
   */
  filename?: string;
}
