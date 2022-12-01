import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { NoSQLService } from './nosql.service';

async function bootstrap() {

  await  NoSQLService.initDb();
  await  NoSQLService.createDbTable();


  const app = await NestFactory.create(AppModule);
  await app.listen(3000);
}
bootstrap();
