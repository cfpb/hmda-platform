create or replace function derive_race_ethnicity(
	race_app_1 text, race_app_2 text, race_app_3 text, race_app_4 text, race_app_5 text,
	race_co_app_1 text, race_co_app_2 text, race_co_app_3 text, race_co_app_4 text, race_co_app_5 text,
	eth_app_1 text, eth_app_2 text, eth_app_3 text, eth_app_4 text, eth_app_5 text,
	eth_co_app_1 text, eth_co_app_2 text, eth_co_app_3 text, eth_co_app_4 text, eth_co_app_5 text
) returns character varying language plpgsql as $$
declare
	race_ethnicity character varying;
	race_app_3_through_5_empty boolean := race_app_3 in ('0', '') and race_app_4 in ('0', '') and race_app_5 in ('0', '');
	race_app_2_through_5_empty boolean := race_app_2 in ('0', '') and race_app_3_through_5_empty;
	race_co_app_3_through_5_empty boolean := race_co_app_3 in ('0', '') and race_co_app_4 in ('0', '') and race_co_app_5 in ('0', '');
	race_co_app_2_through_5_empty boolean := race_co_app_2 in ('0', '') and race_co_app_3_through_5_empty;
	any_race_co_app_white boolean := race_co_app_1 = '5' or race_co_app_2 = '5' or race_co_app_3 = '5' or race_co_app_4 = '5' or race_co_app_5 = '5';
	any_race_app_islander boolean := left(race_app_1, 1) = '4' or left(race_app_2, 1) = '4' or left(race_app_3, 1) = '4' or left(race_app_4, 1) = '4' or left(race_app_5, 1) = '4';
	any_race_app_black boolean := race_app_1 = '3' or race_app_2 = '3' or race_app_3 = '3' or race_app_4 = '3' or race_app_5 = '3';
	any_race_app_native boolean := race_app_1 = '1' or race_app_2 = '1' or race_app_3 = '1' or race_app_4 = '1' or race_app_5 = '1';
	eth_app_2_through_5_empty boolean := eth_app_2 in ('0', '') and eth_app_3 in ('0', '') and eth_app_4 in ('0', '') and eth_app_5 in ('0', '');
	eth_co_app_not_hispanic boolean := eth_co_app_1 = '2' or eth_co_app_2 = '2' or eth_co_app_3 in ('0', '') or eth_co_app_4 in ('0', '') or eth_co_app_5 in ('0', '');
	eth_app_only_hispanic boolean := (left(eth_app_1, 1) = '1' or left(eth_app_2, 1) = '1' or left(eth_app_3, 1) = '1' or left(eth_app_4, 1) = '1' or left(eth_app_5, 1) = '1')
							and not (eth_app_1 = '2' or eth_app_2 = '2' or eth_app_3 = '2' or eth_app_4 = '2' or eth_app_5 = '2');
	eth_co_app_contains_not_hispanic boolean := eth_co_app_1 = '2' or eth_co_app_2 = '2' or eth_co_app_3 = '2' or eth_co_app_4 = '2' or eth_co_app_5 = '2';
begin
	if (left(eth_app_1, 1) = '1' and eth_app_2_through_5_empty and eth_co_app_not_hispanic)
		or (eth_app_only_hispanic and not eth_co_app_contains_not_hispanic) then
		race_ethnicity := 'h';
	elseif race_app_1 = '5' and race_app_2_through_5_empty
		and race_co_app_2_through_5_empty
		and race_co_app_1 in ('5', '6', '7', '8', '') then
		race_ethnicity := 'w';
	elseif (left(race_app_1, 1) = '2' and not race_app_2_through_5_empty and not any_race_co_app_white)
		or (left(race_app_1, 1) = '2' and not any_race_app_islander and not any_race_app_black and not any_race_app_native)
		or (
			((left(race_app_1, 1) = '2' and race_app_2 = '5') or (race_app_1 = '5' and left(race_app_2, 1) = '2'))
			and race_app_3_through_5_empty and not any_race_co_app_white
			) then
		race_ethnicity := 'a';
	elseif (race_app_1 = '3' and race_app_2_through_5_empty and not any_race_co_app_white)
		or (
			((race_app_1 = '3' and race_app_2 = '5') or (race_app_1 = '5' and race_app_2 = '3'))
			and race_app_3_through_5_empty and not any_race_co_app_white
			) then
		race_ethnicity := 'b';
	end if;

	return race_ethnicity;
end;
$$;