Begin
   Num_Infected_Wuhan := 15000; num_of_infected := 0;
   num_of_cured  := 700;   num_fatalities := 500;
BEGIN
   Growing := num_of_cured > num_of_infected;
   Future := num_fatalities / Num_Infected_Wuhan * Num_Infected_Wuhan - (num_fatalities+num_of_cured);
   Better := Num_Infected_Wuhan < ( num_of_cured + num_fatalities );
   daysleft := Num_Infected_Wuhan < (( num_of_cured + num_fatalities * 10 ) AND 
	(num_of_cured < num_fatalities) Or (num_fatalities > num_of_cured))
end
End
.

