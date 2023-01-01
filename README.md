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

#### 7. To change default speed of a metro line, use command.

Automatically accelerates the generated minecart when it travels on a long continuous stretch of powered rails.

Use `/metro speed [Line Name]` to display current speed of a metro line.

Use `/metro speed [Line Name] [double value]` to change the speed of a metro line.

Default speed of powered rails: `0.4`.

Recommended max speed: No more than `2.0`.

### New Feature: Router (above v1.4)

Put a wallsign below the rail

```
_____*___
#########
####]####
```

If `_` is powered rail, `*` is a rail used for router, `]` is a wallsign, `#` are some solid blocks, the shape of rail will change when a minecart arrives.

Attention: Only the wallsign that facing the direction where the cart comes will be applied.

Then edit it as below.

```
[Metro:router]
(string) <-- rule
(string) <-- rule
(string) <-- rule
```

You can add up to 3 rules.

Rules are Like:

```
0-3,6,9SW
4,5N
SE
```

That means:

- When a minecart whose destination is Station 0, 1, 2, 3, 6, 9 arrived, The shape of the rail will be changed to `SW` (A shape that connects to the south and west rails).

- When a minecart whose destination is Station 4, 5 arrived, The shape of the rail will be changed to `N` (A shape that connects to the north and the south) (Since `N` is equal to `S`, you can use `S` if you like).

- When a minecart whose destination is not list above, The shape of the rail will be changed to `SE` (A shape that connects to the south and east rails).

If you don't set default rule (like `SE`, no number before the direction), the rail will not be changed.

Also, `AE` (ascending east), `AS`, `AW`, `AN`, `DE` (descending east), `DS`, `DW`, `DN` are supported.

### Warning

Please use different rails in (near) a metro station to prevent the minecart from going too fast and not stopping at the station. Acceleration will ignore the router.

## Permissions

### metro.reload

- Reload plugin config.

- Enable/Disable debug mode.
  
Default: OP

### metro.speed

- Change the speed of a metro line.

Default: OP

### metro.create

- Create a new router.

- Create a new metro station / line.

- Remove a metro station / line.

Default: OP

### metro.use

- Use the metro station.

Default: All Players
