import { HttpParams } from '@angular/common/http';

type SerializableParam = string | number | boolean | undefined | null;

export function buildHttpParams<T extends object>(
  params: T,
): HttpParams {
  let httpParams = new HttpParams();

  Object.entries(params as Record<string, SerializableParam>).forEach(([key, value]) => {
    if (value !== undefined && value !== null && `${value}`.trim() !== '') {
      httpParams = httpParams.set(key, `${value}`);
    }
  });

  return httpParams;
}
