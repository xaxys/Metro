# Metro
 A simple metro plugin in Minecraft

## How to use

#### 1. Put a wallsign above the rail and edit it as below.

WallSign Format

```
[Metro]
(string) <-- Line Name
(number+[N|S|W|E]) <-- Station Index in Line & NextSation Direction
(string) <-- Station Name
```

Example

```
[Metro]
Line 1
1S
Central Street
```

That means:

The station belongs to `Line 1` .

It is the second station in `Line 1` (count from 0).

To the next station, cart goes south.

The station is named `Central Street`.

#### 2. Right click the sign with things in hand to select stations.

#### 3. Right click the sign with empty hand or sneaking to spawn a minecart.

#### 4. Enter the minecart (start automatically) and enjoy the trip.

#### 5. When you exit the minecart, it will be removed automatically.

#### 6. To setup a loop line, use command.

Use `/metro loop [Line Name]` to check if a line is loop line.

Use `/metro loop [Line Name] [true|false]` to switch a line to loop / straight line.

#### 6. To change default speed of a metro line, use command.

Automatically accelerates the generated minecart when it travels on a long continuous stretch of powered rails.

Use `/metro speed [Line Name]` to display current speed of a metro line.

Use `/metro speed [Line Name] [double value]` to change the speed of a metro line.

Default speed of powered rails: `0.4`.

Recommended max speed: No more than `2.0`.

##### Warning

Please use different rails in (near) a metro station to prevent the minecart from going too fast and not stopping at the station.

## Permissions

### metro.reload

* Reload plugin config.

* Enable/Disable debug mode.
  
Default: OP

### metro.create

* Change the speed of a metro line.

* Create a new metro station / line.

* Remove a metro station / line.

Default: OP

### metro.use

* Use the metro station.

Default: All Players
