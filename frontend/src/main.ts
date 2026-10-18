import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { Shell } from './app/shell/shell';

bootstrapApplication(Shell, appConfig).catch((error) => console.error(error));
