create function sum_json_array ( input_array json )
returns double
language sql deterministic contains sql
BEGIN
    DECLARE array_length INTEGER(11);
    DECLARE retval DOUBLE(19,8);
    DECLARE cell_value DOUBLE(19,8);
    DECLARE idx INT(11);

    SELECT json_length( input_array ) INTO array_length;

    SET retval = 0.0;

    SET idx = 0;

    WHILE idx < array_length DO
        SELECT json_extract( input_array, concat( '$[', idx, ']' ) )
            INTO cell_value;

        SET retval = retval + cell_value;
        SET idx = idx + 1;
    END WHILE;

    RETURN retval;
END