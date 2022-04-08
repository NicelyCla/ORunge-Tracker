
CREATE TABLE IF NOT EXISTS Athlete (
NumeroAthlete int NOT NULL AUTO_INCREMENT,
Nickname varchar(25),
eta int,
PRIMARY KEY (NumeroAthlete)

);


CREATE TABLE IF NOT EXISTS Profile (
Nome varchar(35),
NumeroProfilo int NOT NULL AUTO_INCREMENT,
DataCreazione date,
DataConclusione date,
creatoDa varchar(25),
foreign key(CreatoDa) references Athlete(Nickname),
PRIMARY KEY (NumeroProfilo)

);


CREATE TABLE IF NOT EXISTS Work (
NumeroAllenamento int NOT NULL AUTO_INCREMENT,
DateOfWork date,
StartTime time,
StopTime time,
distance int,
averageSpeed int,
maximunSpeed int,
workedByProfile int,
foreign key(workedByProfile) references Profie(NumeroProfilo),
PRIMARY KEY (NumeroAllenamento)

);
